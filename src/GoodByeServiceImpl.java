public class GoodByeServiceImpl implements GoodByeService{

    @Override
    public String sayGoodBye(String name) {
        return "See you next time," + name;
    }
}
