// SPDX-License-Identifier: GPL-3.0-only
package mod;

import best.at;
import best.ah;
import best.I;
import best.L;
import best.F;
import best.H;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import static mod.CompetitionMod.*;

/** One presentation for every competition, preserving the native phase renderer. */
@SuppressWarnings({"unchecked","rawtypes"})
final class UnifiedTables {
    static final ThreadLocal<Boolean> NATIVE=ThreadLocal.withInitial(()->false);
    static final ThreadLocal<Boolean> CONSTRUCTING=ThreadLocal.withInitial(()->false);
    static final String[] COMMON={"Tabela e fases","Partidas","Artilharia","Notas","Estatísticas","Títulos","Premiações e ranking"};
    static final Color BLUE=new Color(0,69,98);
    static final class Game {
        final I match;final String date,phase;
        Game(I match,String date,String phase){this.match=match;this.date=date;this.phase=phase;}
    }
    static boolean custom(at cup){return cup instanceof ViewCup&&(!(cup instanceof Intercontinental)||year()>=Rules.INTERCONTINENTAL_FIRST_YEAR);}
    static JPanel message(String text){
        JPanel panel=new JPanel(new BorderLayout());JLabel label=new JLabel(text,SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(24,16,24,16));panel.add(label,BorderLayout.NORTH);return panel;
    }
    static String name(ah club){return club==null?"—":club.getNome();}
    static List<Game> games(at cup){
        LinkedHashMap<I,Game> result=new LinkedHashMap<>();
        Set<L> phases=Collections.newSetFromMap(new IdentityHashMap<>());
        L[] original=cup.mB();if(original!=null)for(L phase:original)if(phase!=null)phases.add(phase);
        if(custom(cup)){
            ViewCup view=(ViewCup)cup;if(view.groups()!=null)phases.add(view.groups());
            for(Stage stage:view.stages())phases.add(stage);
        }
        for(Object object:c.a.SR.R()){
            best.a date=(best.a)object;
            for(Object entry:date.h()){
                I match=(I)entry;
                if(match.hy()==cup||phases.contains(match.ht()))
                    result.put(match,new Game(match,date.f(),match.ht()==null?"—":match.ht().io()));
            }
        }
        if(custom(cup)){
            ViewCup view=(ViewCup)cup;
            if(view.groups()!=null)for(I match:view.groups().played)result.putIfAbsent(match,new Game(match,"—","Fase de grupos"));
            for(Stage stage:view.stages())for(Object round:stage.zp())for(Object object:((f.G)round).zW()){
                I match=(I)object;result.putIfAbsent(match,new Game(match,"—",stage.label));
            }
        }
        return new ArrayList<>(result.values());
    }
    static JScrollPane rows(String[] columns,List<Object[]> rows,String empty){
        DefaultTableModel model=new DefaultTableModel(rows.toArray(new Object[0][]),columns){
            public boolean isCellEditable(int row,int column){return false;}
            public Class<?> getColumnClass(int column){
                Class<?> type=null;
                for(Object[] row:rows)if(row[column]!=null){
                    if(type==null)type=row[column].getClass();else if(type!=row[column].getClass())return Object.class;
                }
                return type==null?String.class:type;
            }
        };
        JTable table=new JTable(model);table.setRowHeight(26);table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);table.setBackground(new Color(229,232,234));table.setGridColor(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);table.getTableHeader().setBackground(Color.BLACK);table.getTableHeader().setForeground(Color.WHITE);
        for(int n=0;n<columns.length;n++){
            TableColumn col=table.getColumnModel().getColumn(n);
            if(Arrays.asList("PG","J","V","E","D","GP","GC","SG","G","Nota","Ano","Placar").contains(columns[n])){col.setPreferredWidth(52);col.setMaxWidth(80);}
            else col.setPreferredWidth(170);
        }
        JScrollPane scroll=new JScrollPane(table);scroll.setColumnHeaderView(table.getTableHeader());
        if(rows.isEmpty()){
            scroll.setViewportView(message(empty));scroll.setColumnHeaderView(table.getTableHeader());
        }
        return scroll;
    }
    static JComponent nativeClassification(at cup){
        L[] phases=cup.mB();boolean available=false;
        if(phases!=null)for(L phase:phases)if(phase!=null)available=true;
        if(!available)return message("A classificação será exibida quando as fases desta competição forem definidas.");
        a.ay donor;
        NATIVE.set(true);
        try{
            CONSTRUCTING.set(true);
            try{donor=new a.ay(null,cup);}finally{CONSTRUCTING.remove();}
            // The native constructor omits Nations League types 13/14 from
            // its category mapping, and silently selects the first league.
            // Bind the exact object instead of relying on that mapping.
            NativeUi.set(donor,"xa",true);
            ((JComboBox)member(donor,"va")).setModel(new DefaultComboBoxModel(new Object[]{cup}));
            NativeUi.set(donor,"cS",new ArrayList<>(Collections.singletonList(cup)));
            NativeUi.set(donor,"wU",cup);NativeUi.set(donor,"wY",null);NativeUi.set(donor,"wX",null);
            if(custom(cup))NativeUi.set(donor,"wA",true);
            NativeUi.set(donor,"xa",false);
            java.lang.reflect.Method dispatch=a.ay.class.getDeclaredMethod("cC",int.class);
            dispatch.setAccessible(true);dispatch.invoke(donor,0);
            if(custom(cup)){
                JComboBox phaseBox=(JComboBox)member(donor,"xz");
                ArrayList<components.y> options=new ArrayList<>();
                for(L phase:phases)if(phase!=null)options.add(new components.y(new L[]{phase},phase instanceof Groups?"Fase de grupos":phase.io()));
                NativeUi.set(donor,"wZ",true);
                phaseBox.setModel(new DefaultComboBoxModel(options.toArray()));
                NativeUi.set(donor,"wZ",false);
                phaseBox.setSelectedIndex(0);
            }
            if(member(donor,"wU")!=cup)throw new IllegalStateException("Classificação incompatível com "+cup.getNome());
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Falha ao selecionar "+cup.getNome(),error);}
        finally{NATIVE.remove();}
        JPanel result=new JPanel(new BorderLayout(0,8));result.setBackground(BLUE);
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));toolbar.setOpaque(false);
        JComboBox phasesBox=(JComboBox)member(donor,"xz");
        if(phasesBox.getItemCount()>0){
            phasesBox.setVisible(true);phasesBox.setPreferredSize(new Dimension(230,26));
            JLabel caption=new JLabel("Fase:");caption.setForeground(Color.WHITE);toolbar.add(caption);toolbar.add(phasesBox);
        }
        for(String field:new String[]{"xI","xH"}){
            JLabel action=(JLabel)member(donor,field);if(field.equals("xI"))action.setText("Classificação");if(action.isVisible())toolbar.add(action);
        }
        result.add(toolbar,BorderLayout.NORTH);
        result.add((JScrollPane)member(donor,"ut"),BorderLayout.CENTER);
        result.putClientProperty("mod.nativeDonor",donor);
        return result;
    }
    static JComponent classification(at cup,List<Game> games){
        return nativeClassification(cup);
    }
    static JComponent matches(List<Game> games){
        List<Object[]> data=new ArrayList<>();
        for(Game game:games){I m=game.match;data.add(new Object[]{game.phase,game.date,name(m.hc()),m.e()?m.hu()+" × "+m.hw():"—",name(m.hd()),m.e()?"Encerrada":"A disputar"});}
        return rows(new String[]{"Fase / rodada","Data","Mandante","Placar","Visitante","Situação"},data,"Nenhuma partida agendada para esta competição nesta temporada.");
    }
    static String playerClub(F player,at cup){
        if(best.aq.fs(cup.b()))return best.ac.bl(player.getPais());
        return name(player.fg());
    }
    static JComponent scorers(at cup){
        List<best.c> sorted=new ArrayList<>();if(cup.ms()!=null)for(Object object:cup.ms())if(object instanceof best.c)sorted.add((best.c)object);
        sorted.sort(Comparator.comparingInt(best.c::y).reversed().thenComparing(x->x.x()==null?"":x.x().getNome()));
        List<Object[]> data=new ArrayList<>();
        for(best.c score:sorted)if(score.x()!=null&&score.y()>0)data.add(new Object[]{score.x().getNome(),playerClub(score.x(),cup),score.y(),score.A()});
        return rows(new String[]{"Jogador","Clube / seleção","G","J"},data,"Ainda não há gols registrados nesta competição.");
    }
    static JComponent ratings(at cup){
        List<Object[]> data=new ArrayList<>();
        // Read the same player records as the original screen; retain all rated players.
        for(Object object:c.a.SR.O()){
            F player=(F)object;H score=player.g(cup);
            if(score!=null&&score.gZ()>0&&score.F()>0)data.add(new Object[]{player.getNome(),playerClub(player,cup),Math.round(score.F()*100.0)/100.0,score.gZ()});
        }
        data.sort((x,y)->Double.compare((Double)y[2],(Double)x[2]));
        return rows(new String[]{"Jogador","Clube / seleção","Nota","J"},data,"Ainda não há notas registradas nesta competição.");
    }
    static LinkedHashMap<ah,Integer> winners(at cup){
        LinkedHashMap<ah,Integer> titles=new LinkedHashMap<>();
        for(Object object:cup.mn()){best.i title=(best.i)object;if(title.ce()!=null)titles.merge(title.ce(),1,Integer::sum);}return titles;
    }
    static JComponent statistics(at cup,List<Game> games){
        int played=0,goals=0;I biggest=null;
        for(Game game:games)if(game.match.e()){
            I match=game.match;played++;goals+=match.hu()+match.hw();
            if(biggest==null||Math.abs(match.hu()-match.hw())>Math.abs(biggest.hu()-biggest.hw()))biggest=match;
        }
        List<Object[]> data=new ArrayList<>();
        data.add(new Object[]{"Temporada",year()});data.add(new Object[]{"Partidas disputadas",played});
        data.add(new Object[]{"Partidas agendadas",games.size()-played});data.add(new Object[]{"Gols",goals});
        data.add(new Object[]{"Média de gols",played==0?"Ainda sem jogos":String.format(new Locale("pt","BR"),"%.2f",(double)goals/played)});
        data.add(new Object[]{"Maior goleada",biggest==null?"Ainda sem jogos":name(biggest.hc())+" "+biggest.hu()+" × "+biggest.hw()+" "+name(biggest.hd())});
        best.i last=null;for(Object object:cup.mn()){best.i title=(best.i)object;if(last==null||title.H()>last.H())last=title;}
        data.add(new Object[]{"Último campeão",last==null?"Ainda não definido":name(last.ce())+" ("+(c.a.SR.op()+last.H()-1)+")"});
        List<Map.Entry<ah,Integer>> titles=new ArrayList<>(winners(cup).entrySet());titles.sort((a,b)->Integer.compare(b.getValue(),a.getValue()));
        for(Map.Entry<ah,Integer> entry:titles)data.add(new Object[]{"Títulos — "+name(entry.getKey()),entry.getValue()});
        return rows(new String[]{"Estatística / histórico","Valor"},data,"");
    }
    static JComponent titles(at cup){
        List<Object[]> data=new ArrayList<>();List<at> cups=new ArrayList<>();cups.add(cup);
        if(cup instanceof Intercontinental&&custom(cup)){Intercontinental root=(Intercontinental)cup;Collections.addAll(cups,root.afro,root.americas,root.challenger);}
        for(at titleCup:cups)for(Object object:titleCup.mn()){
            best.i title=(best.i)object;data.add(new Object[]{c.a.SR.op()+title.H()-1,titleCup.getNome(),name(title.ce()),name(title.cf())});
        }
        data.sort((x,y)->Integer.compare((Integer)y[0],(Integer)x[0]));
        return rows(new String[]{"Ano","Competição","Campeão","Vice-campeão"},data,"Nenhum título registrado para esta competição.");
    }
    static JPanel create(at cup){
        List<Game> games=games(cup);
        JPanel panel=new JPanel(new BorderLayout(0,8));panel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));panel.setBackground(BLUE);
        JPanel heading=new JPanel(new BorderLayout(12,0));heading.setOpaque(false);
        ImageIcon image=NativeUi.image(cup);
        if(image!=null)heading.add(new JLabel(best.aq.a(image,42,62)),BorderLayout.WEST);
        JLabel title=new JLabel(cup.getNome());title.setFont(title.getFont().deriveFont(Font.BOLD,18));title.setForeground(Color.WHITE);
        JPanel text=new JPanel(new GridLayout(2,1));text.setOpaque(false);text.add(title);
        String status=cup instanceof WorldCup?((WorldCup)cup).status:"Temporada "+year()+" • "+games.stream().filter(g->g.match.e()).count()+" jogos disputados • "+games.size()+" jogos no calendário";
        JLabel sub=new JLabel(status);sub.setForeground(Color.WHITE);text.add(sub);heading.add(text,BorderLayout.CENTER);panel.add(heading,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add(COMMON[0],classification(cup,games));tabs.add(COMMON[1],matches(games));tabs.add(COMMON[2],scorers(cup));tabs.add(COMMON[3],ratings(cup));
        tabs.add(COMMON[4],statistics(cup,games));tabs.add(COMMON[5],titles(cup));
        if(Rewards.id(cup)!=null&&custom(cup))rewardsTab(tabs,cup);
        else tabs.add(COMMON[6],message("Esta competição segue as premiações e os pontos de ranking do regulamento original do jogo."));
        if(cup instanceof WorldCup){
            List<Object[]> entries=new ArrayList<>();for(Qualified q:((WorldCup)cup).qualified)entries.add(new Object[]{name(q.club),Rules.CONFEDERATIONS[q.club.gg()],q.points,q.reason});
            tabs.add("Vagas do Mundial",rows(new String[]{"Clube","Confederação","Pontos","Vaga"},entries,"Classificados ainda não definidos para a próxima edição."));
        }
        panel.add(tabs,BorderLayout.CENTER);panel.putClientProperty("mod.unifiedTabs",tabs);panel.putClientProperty("mod.competition",cup);return panel;
    }
}
